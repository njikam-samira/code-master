/*
 * Copyright 2013 Jordi Casas-Roma, Alexandre Dotor Casals
 * 
 * This file is part of UMGA. 
 * 
 * UMGA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UMGA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with UMGA.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.uoc.kison.UMGA.objects;

import java.util.Arrays;

public class Group {

    private int[] values;
    private int floorDiffs;
    private int ceilDiffs;
    private int floorMean;
    private int ceilMean;
    public final static int floorDiff = 1;
    public final static int ceilDiff = 2;

    public Group(int[] values) {
        this.values = values;

        int sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        double mitjana = sum / (double) values.length;

        this.floorMean = (int) Math.floor(mitjana);
        this.ceilMean = (int) Math.ceil(mitjana);

        this.floorDiffs = 0;
        this.ceilDiffs = 0;
        for (int i = 0; i < values.length; i++) {
            this.floorDiffs += values[i] - this.floorMean;
            this.ceilDiffs += values[i] - this.ceilMean;
        }

    }

    @Override
    public String toString() {
        return "Group [valors=" + Arrays.toString(values) + ", floor=" + floorDiffs
                + ", ceil=" + ceilDiffs + "]";
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] valors) {
        this.values = valors;
    }

    public int getFloorDiffs() {
        return floorDiffs;
    }

    public void setFloorDiffs(int floorDiffs) {
        this.floorDiffs = floorDiffs;
    }

    public int getCeilDiffs() {
        return ceilDiffs;
    }

    public void setCeilDiffs(int ceilDiffs) {
        this.ceilDiffs = ceilDiffs;
    }

    public int getFloorMean() {
        return floorMean;
    }

    public void setFloorMean(int floorMean) {
        this.floorMean = floorMean;
    }

    public int getCeilMean() {
        return ceilMean;
    }

    public void setCeilMean(int ceilMean) {
        this.ceilMean = ceilMean;
    }

    public boolean hasOption() {
        return ceilDiffs != floorDiffs;
    }
}
