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

import org.jtransforms.fft.DoubleFFT_2D;

class ReconstructionFieldImpl implements ReconstructionField {
    public ReconstructionFieldImpl(double[][] real, double[][] imag)
    {
        int width = real.length;
        int height = real[0].length;
        M_fft = new DoubleFFT_2D(width, height);
        double[][] field = new double[width][height * 2];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                field[x][2*y] = real[x][y];
                field[x][2*y+1] = imag[x][y];
            }
        }
        //M_field = new ReconstructionComplexField(this, field);
    }
    public void field_changed(ReconstructionComplexField field)
    {
        if (field == M_field) M_fourier = null;
        if (field == M_fourier) M_field = null;
    }
    @Override
    public ReconstructionComplexField field()
    {
        if (!has_field()) {
            M_field = M_fourier.copy();
            M_field.shift();
            M_fft.complexInverse(M_field.M_field, true);
        }
        return M_field;
    }
    @Override
    public ReconstructionComplexField fourier()
    {
        if (!has_fourier()) {
            M_fourier = M_field.copy();
            M_fft.complexForward(M_fourier.M_field);
            M_fourier.shift();
        }
        return M_field;
    }
    @Override public boolean has_field()   {return M_field   != null;}
    @Override public boolean has_fourier() {return M_fourier != null;}

    private DoubleFFT_2D M_fft;
    private ReconstructionComplexField M_field;
    private ReconstructionComplexField M_fourier;
}
