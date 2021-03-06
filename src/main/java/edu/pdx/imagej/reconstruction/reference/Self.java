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

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import edu.pdx.imagej.reconstruction.ConstReconstructionField;
import edu.pdx.imagej.reconstruction.ReconstructionField;

/** A {@link ReferencePlugin} that uses the current field as the reference
 * hologram.  The {@link edu.pdx.imagej.reconstruction.filter.Filter Filter}
 * needs to be different in this case.
 */
@Plugin(type = ReferencePlugin.class,
        name = "Self",
        priority = Priority.VERY_HIGH * 0.9)
public class Self extends AbstractReferencePlugin {
    /** Get the reference hologram.  This just copies the field.
     *
     * @param field The filtered field.
     * @param t Unused.
     */
    @Override
    public ReconstructionField getReferenceHolo(
        ConstReconstructionField field, int t)
    {
        return field.copy();
    }
    @Override
    public Self duplicate()
    {
        return new Self();
    }
    /** Returns <code>true</code>.
     *
     * @return <code>true</code>.
     */
    @Override
    public boolean dontUseSameRoi() {return true;}
}
