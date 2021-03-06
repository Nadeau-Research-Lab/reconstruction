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

package edu.pdx.imagej.reconstruction.poly_tilt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.ArrayList;

import edu.pdx.imagej.reconstruction.plugin.ReconstructionPlugin;

public class AutoTest {
    @Test public void testSmall()
    {
        Auto test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[2][2];
        S_pt.M_degree = 1;
        Iterable<Point> line = test.getHLine();
        int i = 0;
        for (Point p : line) ++i;
        assertTrue(i != 0, "Auto should never create no line.");

        test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[6][6];
        line = test.getHLine();
        i = 0;
        for (Point p : line) ++i;
        assertEquals(4, i);
    }
    @Test public void testPerfectLinear()
    {
        Auto test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[][]{
            {0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0},
            {0, 1, 2, 3, 0},
            {0, 1, 0, 1, 0},
            {0, 1, 0, 1, 0},
        };
        S_pt.M_degree = 1;
        Iterable<Point> line = test.getVLine();
        int i = 1;
        for (Point p : line) {
            assertEquals(i++, p.y);
            assertEquals(2, p.x);
        }
        assertEquals(4, i);
    }
    @Test public void testPerfectQuadratic()
    {
        Auto test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[][]{
            {0, 1  , 2  , 0  , 1  , 2},
            {0, 1  , 2  , 0  , 1  , 2},
            {0, 1  , 2  , 0  , 1  , 2},
            {0, 1  , 2  , 0  , 1  , 2},
            {0, 0.1, 0.4, 0.9, 1.6, 2},
            {0, 1  , 2  , 0  , 1  , 2},
        };
        S_pt.M_degree = 2;
        Iterable<Point> line = test.getVLine();
        int i = 1;
        for (Point p : line) {
            assertEquals(i++, p.y);
            assertEquals(4, p.x);
        }
        assertEquals(5, i);
    }
    @Test public void testNonperfect()
    {
        Auto test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[][]{
            {0, 1  , 2  , 0, 1  , 2},
            {0, 1  , 2  , 0, 1  , 2},
            {0, 1  , 2  , 0, 1  , 2},
            {0, 1  , 2  , 0, 1  , 2},
            {0, 0.1, 0.3, 1, 1.5, 2},
            {0, 1  , 2  , 0, 1  , 2},
        };
        S_pt.M_degree = 2;
        Iterable<Point> line = test.getVLine();
        int i = 1;
        for (Point p : line) {
            assertEquals(i++, p.y);
            assertEquals(4, p.x);
        }
        assertEquals(5, i);
    }
    @Test public void testPhase()
    {
        Auto test = new Auto();
        test.readPlugins(S_plugins);
        S_pt.M_phase = new double[][]{
            {0, 1  , 2  , 0  ,  1  , 2},
            {0, 1  , 2  , 0  ,  1  , 2},
            {0, 1  , 2  , 0  ,  1  , 2},
            {0, 1  , 2  , 0  ,  1  , 2},
            {0, 0.2, 0.8, 1.8, -3.1, 2},
            {0, 1  , 2  , 0  ,  1  , 2},
        };
        S_pt.M_degree = 2;
        Iterable<Point> line = test.getVLine();
        int i = 1;
        for (Point p : line) {
            assertEquals(i++, p.y);
            assertEquals(4, p.x);
        }
        assertEquals(5, i);
    }
    private static PolyTilt S_pt = new PolyTilt();
    private static ArrayList<ReconstructionPlugin> S_plugins = getPlugins();
    private static ArrayList<ReconstructionPlugin> getPlugins()
    {
        ArrayList<ReconstructionPlugin> plugins = new ArrayList<>();
        plugins.add(S_pt);
        return plugins;
    }
}
