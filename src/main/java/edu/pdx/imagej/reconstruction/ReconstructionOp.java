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

package edu.pdx.imagej.reconstruction;

import java.util.Collections;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.scijava.Contextual;
import org.scijava.Prioritized;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.ops.AbstractOp;
import net.imagej.ops.Op;

import edu.pdx.imagej.reconstruction.units.DistanceUnitValue;
import edu.pdx.imagej.reconstruction.plugin.ReconstructionPlugin;

/** An Op that calculates the entire reconstruction pipeline with any of the
 * plugins passed in.
 * <p>
 * To run this op yourself, it has the name "Hologram Reconstruction" and has
 * these parameters:
 * <ol>
 *      <li>Hologram: An <code>ImagePlus</code> representing your hologram
 *                    (stack) you wish to reconstruct.
 *      <li>Wavelength: A {@link
 *          edu.pdx.imagej.reconstruction.units.DistanceUnitValue
 *          DistanceUnitValue} representing the wavelength of light used to get
 *          this hologram.
 *      <li>Width: A {@link
 *          edu.pdx.imagej.reconstruction.units.DistanceUnitValue
 *          DistanceUnitValue} representing the real width of the hologram.
 *      <li>Height: A {@link
 *          edu.pdx.imagej.reconstruction.units.DistanceUnitValue
 *          DistanceUnitValue} representing the real height of the hologram.
 *      <li>Ts: A <code>List&lt;Integer&gt;</code> representing the time slices
 *              you want to reconstruct.  Each time is assumed to be in the
 *              range 1 &le; t &le; number of slices in hologram.
 *      <li>Zs: A <code>List&lt;{@link
 *              edu.pdx.imagej.reconstruction.units.DistanceUnitValue
 *              distanceUnitValue}&gt;</code> of all z slices you want to
 *              propagate to.
 *      <li>Plugins: A <code>List&lt;{@link
 *                   edu.pdx.imagej.reconstruction.plugin.ReconstructionPlugin
 *                   ReconstructionPlugin}&gt;</code> of all plugins you wish to
 *                   use.
 * </ol>
 * There is no output for this op.  If you want to get the result, please use
 * {@link edu.pdx.imagej.reconstruction.result.Result Result}.
 * <p>
 * If you want to add something to this process, please look at {@link
 * edu.pdx.imagej.reconstruction.plugin}.
 */
@Plugin(type = Op.class, name = "Hologram Reconstruction")
public class ReconstructionOp extends AbstractOp {
    @Parameter private ImagePlus                  P_hologram;
    @Parameter private DistanceUnitValue          P_wavelength;
    @Parameter private DistanceUnitValue          P_width;
    @Parameter private DistanceUnitValue          P_height;
    @Parameter private List<Integer>              P_ts;
    @Parameter private List<DistanceUnitValue>    P_zs;
    @Parameter private List<ReconstructionPlugin> P_plugins;

    @Parameter private StatusService P_status;

    @Override
    public void run()
    {
        // If the plugins were created by the programmer, they have no context,
        // and thus no priority.  We need to inject their context if needed.
        for (ReconstructionPlugin plugin : P_plugins) {
            if (plugin instanceof Contextual) {
                if (((Contextual)plugin).getContext() == null) {
                    ops().context().inject((Contextual)plugin);
                }
            }
            if (plugin instanceof Prioritized) {
                Plugin annotation = plugin.getClass()
                                          .getAnnotation(Plugin.class);
                if (annotation != null) {
                    ((Prioritized)plugin).setPriority(annotation.priority());
                }
            }
        }
        // Beginning
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.setBeginningPriority();
        }
        Collections.sort(P_plugins);
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.readPlugins(P_plugins);
            plugin.processBeforeParam();
            plugin.processHologramParam(P_hologram);
            plugin.processWavelengthParam(P_wavelength);
            plugin.processDimensionsParam(P_width, P_height);
            plugin.processTsParam(P_ts);
            plugin.processZsParam(P_zs);
            plugin.processBeginning();
            if (plugin.hasError()) return;
        }

        // Original Hologram
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.setOriginalHologramPriority();
        }
        Collections.sort(P_plugins);
        ReconstructionField field = createField(P_hologram.getProcessor());
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.processOriginalHologram(
                new ConstReconstructionField(field));
            if (plugin.hasError()) return;
        }

        for (int t : P_ts) {
            // Hologram
            for (ReconstructionPlugin plugin : P_plugins) {
                plugin.setHologramPriority();
            }
            Collections.sort(P_plugins);
            field = createField(
                P_hologram.getStack().getProcessor(t));
            for (ReconstructionPlugin plugin : P_plugins) {
                plugin.processHologram(field, t);
                if (plugin.hasError()) return;
            }

            // Filtered Field
            for (ReconstructionPlugin plugin : P_plugins) {
                plugin.setFilteredFieldPriority();
            }
            Collections.sort(P_plugins);
            for (ReconstructionPlugin plugin : P_plugins) {
                plugin.processFilteredField(field, t);
                if (plugin.hasError()) return;
            }

            // Propagated Field
            for (ReconstructionPlugin plugin : P_plugins) {
                plugin.setPropagatedFieldPriority();
            }
            Collections.sort(P_plugins);
            for (DistanceUnitValue z : P_zs) {
                if (IJ.escapePressed()) {
                    P_status.showStatus(1, 1, "Command canceled");
                    return;
                }
                for (ReconstructionPlugin plugin : P_plugins) {
                    plugin.processPropagatedField(field, t, z);
                    if (plugin.hasError()) return;
                }
            }
        }

        // Ending
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.setEndingPriority();
        }
        Collections.sort(P_plugins);
        for (ReconstructionPlugin plugin : P_plugins) {
            plugin.processEnding();
            if (plugin.hasError()) return;
        }
    }
    private static ReconstructionField createField(ImageProcessor image)
    {
        float[][] floatArray = image.getFloatArray();
        double[][] real = new double[floatArray.length][floatArray[0].length];
        double[][] imag = new double[real.length][real[0].length];
        for (int x = 0; x < real.length; ++x) {
            for (int y = 0; y < real[0].length; ++y) {
                real[x][y] = floatArray[x][y];
            }
        }
        return new ReconstructionFieldImpl(real, imag);
    }
}
