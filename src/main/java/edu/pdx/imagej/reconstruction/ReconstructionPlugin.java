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

import java.util.LinkedHashMap;
import java.util.AbstractList;

import ij.ImagePlus;

import org.scijava.Prioritized;
import net.imagej.ImageJPlugin;

import edu.pdx.imagej.dynamic_parameters.ParameterPlugin;
import edu.pdx.imagej.reconstruction.units.DistanceUnitValue;

public interface ReconstructionPlugin extends ImageJPlugin, ParameterPlugin {
    default void read_plugins(
        LinkedHashMap<Class<?>, ReconstructionPlugin> plugins) {}

    default void process_before_param() {}
    default void process_hologram_param(ImagePlus hologram) {}
    default void process_wavelength_param(DistanceUnitValue wavelength) {}
    default void process_dimensions_param(DistanceUnitValue width,
                                          DistanceUnitValue height) {}
    default void process_ts_param(AbstractList<Integer> ts) {}
    default void process_zs_param(AbstractList<DistanceUnitValue> zs) {}

    default void process_beginning() {}

    default void process_original_hologram(ConstReconstructionField field) {}
    default void process_hologram(ReconstructionField field, int t) {}
    default void process_filtered_field(ReconstructionField field, int t) {}
    default void process_propagated_field(
        ConstReconstructionField original_field,
        ReconstructionField current_field,
        int t, DistanceUnitValue z_from, DistanceUnitValue z_to) {}
    default void process_ending() {}

    default void set_beginning_priority()         {}
    default void set_original_hologram_priority() {}
    default void set_hologram_priority()          {}
    default void set_filtered_field_priority()    {}
    default void set_propagated_field_priority()  {}
    default void set_ending_priority()            {}

    default boolean has_error() {return false;}
}
