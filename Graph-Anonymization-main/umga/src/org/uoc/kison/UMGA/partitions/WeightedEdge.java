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
package org.uoc.kison.UMGA.partitions;

/**
 *
 * @author jcasasr
 */
public class WeightedEdge {
    private int target;
    private int weight;
    
    public WeightedEdge(int target, int weight) {
        this.target = target;
        this.weight = weight;
    }
    
    public int getTarget() {
        return this.target;
    }
    
    public int getWeight() {
        return this.weight;
    }
}
