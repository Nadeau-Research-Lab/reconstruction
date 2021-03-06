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

package edu.pdx.imagej.reconstruction.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.util.AbstractList;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;

import org.scijava.Context;

import edu.pdx.imagej.dynamic_parameters.TestDialog;

import edu.pdx.imagej.reconstruction.ReconstructionField;

public class MedianTest {
    @Test public void testMedian()
    {
        ImageStack stack = new ImageStack(2, 2);
        stack.addSlice(new FloatProcessor(new float[][] {{0, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{382, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{5, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{99, 0}, {0, 0}}));
        ImagePlus imp = new ImagePlus("", stack);

        Median test = new Median();

        ReconstructionField result = test.getReferenceHolo(imp,
            new AbstractList<Integer>() {
                @Override
                public Integer get(int index) {return index * 2 + 1;}
                @Override
                public int size() {return 2;}
            });

        assertEquals(result.field().getReal(0, 0), 2.5);
    }
    @Test public void testLive()
    {
        ImageStack stack = new ImageStack(2, 2);
        stack.addSlice(new FloatProcessor(new float[][] {{0, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{382, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{5, 0}, {0, 0}}));
        stack.addSlice(new FloatProcessor(new float[][] {{99, 0}, {0, 0}}));
        ImagePlus imp = new ImagePlus("", stack);
        Median test = new Median(new ImagePlus[]{imp});
        Context context = new Context(true);
        context.inject(test.param());
        test.param().initialize();
        test.param().refreshVisibility();
        TestDialog dialog = new TestDialog();
        test.param().addToDialog(dialog);
        dialog.getString(0).value = "List";
        dialog.getStringIndex(0).value = 0; // Select the image
        test.param().readFromDialog();
        dialog = new TestDialog();
        test.param().addToDialog(dialog);
        dialog.getString(1).value = "1,3"; // Select the times to use
        dialog.getStringIndex(0).value = 0; // Crashes if we don't do this
        test.param().readFromDialog();

        ReconstructionField field = test.getReferenceHolo(null, 0);
        assertEquals(field.field().getReal(0, 0), 2.5);
    }
}
