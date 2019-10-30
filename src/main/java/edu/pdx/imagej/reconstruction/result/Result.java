/* Copyright (C) 2019 Portland State University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of version 3 of the GNU Lesser General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * For any questions regarding the license, please contact the Free Software
 * Foundation.  For any other questions regarding this program, please contact
 * David Cohoe at dcohoe@pdx.edu.
 */

package edu.pdx.imagej.reconstruction.result;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import edu.pdx.imagej.dynamic_parameters.DParameter;
import edu.pdx.imagej.reconstruction.plugin.ReconstructionPlugin;
import edu.pdx.imagej.reconstruction.plugin.AbstractReconstructionPlugin;
import edu.pdx.imagej.reconstruction.plugin.MainReconstructionPlugin;
import edu.pdx.imagej.reconstruction.ConstReconstructionField;
import edu.pdx.imagej.reconstruction.ReconstructionField;
import edu.pdx.imagej.reconstruction.units.DistanceUnitValue;

/** A {@link edu.pdx.imagej.reconstruction.plugin.ReconstructionPlugin
 * ReconstructionPlugin} that acquires the result and then finds a way to
 * communicate this result to the user.  Currently, you can save it to a file,
 * or you can use a custom function to do what you wish with the result.  The
 * default operation when not saving to file is to call <code>show()</code> on
 * each result.
 */
@Plugin(type = ReconstructionPlugin.class, name = "Result",
        priority = Priority.LAST)
public class Result extends AbstractReconstructionPlugin
                    implements MainReconstructionPlugin {
    @Parameter UIService P_ui;
    /** Constructor intended for live use of the plugin.
     */
    public Result() {}
    /** Constructor intended for programmatic use of the plugin.
     *
     * @param options The {@link ResultOptions} to use to process everything.
     */
    public Result(ResultOptions options)
    {
        M_options = options;
    }

    @Override
    public DParameter param() {return M_param;}
    /** Get the options from the parameter.
     */
    @Override
    public void processBeforeParam()
    {
        if (M_options == null) M_options = M_param.getValue();
    }
    /** Get required information from the hologram.  The name of the hologram,
     * as well as its dimensions, are used.
     */
    @Override
    public void processHologramParam(ImagePlus hologram)
    {
        M_hologram = hologram;
        M_pixelWidth = hologram.getProcessor().getWidth();
        M_pixelHeight = hologram.getProcessor().getHeight();
    }
    /** Get the size of the image to create a <code>Calibration</code> object.
     */
    @Override
    public void processDimensionsParam(DistanceUnitValue width,
                                         DistanceUnitValue height)
    {
        M_cal = new Calibration();
        M_cal.pixelWidth = width.value() / M_pixelWidth;
        M_cal.pixelHeight = height.value() / M_pixelHeight;
        M_cal.setUnit(width.unit().toString());
    }
    /** Determine how many time slices there are.
     */
    @Override
    public void processTsParam(List<Integer> ts)
    {
        M_tSize = ts.size();
    }
    /** Determine how many z slices there are, and create directories if {@link
     * ResultOptions#saveToFile} is <code>true</code>.
     */
    @Override
    public void processZsParam(List<DistanceUnitValue> zs)
    {
        M_zSize = zs.size();
        if (M_options.saveToFile) {
            try {
                for (DistanceUnitValue z : zs) {
                    if (M_options.amplitude) {
                        new File(Paths.get(M_options.saveDirectory,
                            "Amplitude", formatZ(z)).toString()).mkdirs();
                    }
                    if (M_options.phase) {
                        new File(Paths.get(M_options.saveDirectory,
                            "Phase", formatZ(z)).toString()).mkdirs();
                    }
                    if (M_options.real) {
                        new File(Paths.get(M_options.saveDirectory,
                            "Real", formatZ(z)).toString()).mkdirs();
                    }
                    if (M_options.imaginary) {
                        new File(Paths.get(M_options.saveDirectory,
                            "Imaginary", formatZ(z)).toString()).mkdirs();
                    }
                }
            }
            catch (SecurityException e) {
                P_ui.showDialog("Unable to create directories: "
                    + e.getMessage(), "Error");
                M_error = true;
                return;
            }
        }
    }
    /** Create the result images if {@link ResultOptions#saveToFile} is
     * <code>false</code>.
     */
    @Override
    public void processBeginning()
    {
        if (!M_options.saveToFile) {
            if (M_options.amplitude) {
                M_amplitude = new ImageStack(M_pixelWidth, M_pixelHeight);
            }
            if (M_options.phase) {
                M_phase = new ImageStack(M_pixelWidth, M_pixelHeight);
            }
            if (M_options.real) {
                M_real = new ImageStack(M_pixelWidth, M_pixelHeight);
            }
            if (M_options.imaginary) {
                M_imaginary = new ImageStack(M_pixelWidth, M_pixelHeight);
            }
        }
    }
    /** Get the final result.
     */
    @Override
    public void processPropagatedField(ReconstructionField field,
                                         int t, DistanceUnitValue z)
    {
        if (M_options.amplitude) {
            processParticular(field.field().getAmp(), t, z,
                               M_amplitude, "Amplitude", getSliceLabel(t));
        }
        if (M_options.phase) {
            processParticular(field.field().getArg(), t, z,
                               M_phase, "Phase", getSliceLabel(t));
        }
        if (M_options.real) {
            processParticular(field.field().getReal(), t, z,
                               M_real, "Real", getSliceLabel(t));
        }
        if (M_options.imaginary) {
            processParticular(field.field().getImag(), t, z,
                               M_imaginary, "Imaginary", getSliceLabel(t));
        }
    }
    private void processParticular(double[][] dResult, int t,
                                    DistanceUnitValue z, ImageStack stack,
                                    String type, String label)
    {
        float[][] result = new float[dResult.length][dResult[0].length];
        for (int x = 0; x < result.length; ++x) {
            for (int y = 0; y < result[0].length; ++y) {
                result[x][y] = (float)dResult[x][y];
            }
        }
        ImageProcessor proc = new FloatProcessor(result);
        if (M_options.type == ResultOptions.Type.Type8Bit) {
            proc = proc.convertToByteProcessor();
        }
        else if (M_options.type == ResultOptions.Type.Type16Bit) {
            proc = proc.convertToShortProcessor();
        }

        if (M_options.saveToFile) {
            ImagePlus tempImg = new ImagePlus("", proc);
            tempImg.setCalibration(M_cal);
            IJ.saveAsTiff(tempImg, Paths.get(M_options.saveDirectory, type,
                formatZ(z), formatT(t)).toString());
            tempImg.close();
        }
        // Not save to file
        else {
            stack.addSlice(label + ", z = " + formatZ(z), proc);
        }
    }
    /** Show the final result.
     */
    @Override
    public void processEnding()
    {
        almostProcessEnding();
        if (!M_options.saveToFile) {
            if (M_options.amplitude) {
                M_options.amplitudeFunc.accept(M_amplitudeImp);
            }
            if (M_options.phase) M_options.phaseFunc.accept(M_phaseImp);
            if (M_options.real) M_options.realFunc.accept(M_realImp);
            if (M_options.imaginary) {
                M_options.imaginaryFunc.accept(M_imaginaryImp);
            }
        }
    }
    void almostProcessEnding() // Package private for testing
    {
        if (!M_options.saveToFile) {
            if (M_options.amplitude) {
                M_amplitudeImp = createImp(M_amplitude, "Amplitude");
            }
            if (M_options.phase) M_phaseImp = createImp(M_phase, "Phase");
            if (M_options.real) M_realImp = createImp(M_real, "Real");
            if (M_options.imaginary) {
                M_imaginaryImp = createImp(M_imaginary, "Imaginary");
            }
        }
    }
    private ImagePlus createImp(ImageStack stack, String label)
    {
        int bitDepth = 8;
        if (M_options.type == ResultOptions.Type.Type16Bit) bitDepth = 16;
        else if (M_options.type == ResultOptions.Type.Type32Bit) bitDepth = 32;
        ImagePlus imp = IJ.createHyperStack(label, M_pixelWidth,
                                            M_pixelHeight, 1, M_zSize,
                                            M_tSize, bitDepth);
        imp.setStack(stack);
        imp.setCalibration(M_cal);
        return imp;
    }
    @Override public boolean hasError() {return M_error;}
    @Override public Result duplicate()
    {
        processBeforeParam(); // Make sure M_options is not null
        return new Result(M_options);
    }

    private String formatZ(DistanceUnitValue z)
        {return String.format("%.3f", z.value());}
    private String formatT(int t) {return String.format("%05d", t);}
    private String getSliceLabel(int t)
    {
        String result = M_hologram.getImageStack().getSliceLabel(t);
        if (result == null) result = M_hologram.getTitle();
        return result;
    }

    private ResultParameter M_param = new ResultParameter();
    ResultOptions M_options; // Package private for testing
    private ImageStack M_amplitude;
    private ImageStack M_phase;
    private ImageStack M_real;
    private ImageStack M_imaginary;
    ImagePlus M_amplitudeImp; // Package private for testing
    ImagePlus M_phaseImp;
    ImagePlus M_realImp;
    ImagePlus M_imaginaryImp;
    private boolean M_error = false;
    private ImagePlus M_hologram;
    private Calibration M_cal;
    private int M_zSize;
    private int M_tSize;
    private int M_pixelWidth;
    private int M_pixelHeight;
}
