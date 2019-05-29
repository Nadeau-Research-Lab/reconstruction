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

package edu.pdx.imagej.reconstruction.plugin;

import org.scijava.plugin.Plugin;

import edu.pdx.imagej.dynamic_parameters.PluginParameter;
import edu.pdx.imagej.dynamic_parameters.ImageParameter;
import edu.pdx.imagej.dynamic_parameters.DParameter;

@Plugin(type = DParameter.class)
public class ReconstructionPluginParameter<T extends ReconstructionPlugin>
             extends PluginParameter<T> implements HologramPluginParameter {
    public ReconstructionPluginParameter(String label, Class<T> cls)
    {
        super(label, cls);
    }
    @Override
    public void set_hologram(ImageParameter hologram)
    {
        for (T plugin : get_all_plugins()) {
            DParameter<?> param = plugin.param();
            if (param instanceof HologramPluginParameter) {
                ((HologramPluginParameter)param).set_hologram(hologram);
            }
        }
    }
}
