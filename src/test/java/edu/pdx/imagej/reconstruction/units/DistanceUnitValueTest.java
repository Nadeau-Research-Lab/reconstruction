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

package edu.pdx.imagej.reconstruction.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DistanceUnitValueTest {
    @Test public void test_convert()
    {
        DistanceUnitValue val = new DistanceUnitValue(2, DistanceUnits.Milli);
        assertEquals(val.as_meter(), 2e-3);
        val = new DistanceUnitValue(2, DistanceUnits.Meter);
        assertEquals(val.as_milli(), 2e3);
        val = new DistanceUnitValue(2, DistanceUnits.Centi);
        assertEquals(val.as_micro(), 2e4);
    }
}