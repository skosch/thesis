/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.common.util.objects;

import choco.kernel.common.logging.ChocoLogging;
import gnu.trove.TObjectIntHashMap;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a bipartite set.
 * <p/>
 * Cette classe est utilisee pour stocker les evenements de propagation
 * de contrainte : les elements de gauche sont a propages, les autres
 * ne doivent pas etre propages.
 */
public final class BipartiteSet<E> {
    /**
     * Reference to an object for logging trace statements related to util (using the java.util.logging package)
     */

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Contains all the objects in the two parts of the set.
     */

    private E[] objects;
    private int size;


    /**
     * The number of elements in the left part of the set.
     */

    private int nbLeft = 0;


    /**
     * Maps the element objects to the corresponding index.
     */

    private TObjectIntHashMap<E> indices;


    /**
     * Constructs a new bipartite set. Initialized internal util.
     */

    public BipartiteSet() {
        this.objects =(E[]) new Object[16];
        this.indices = new TObjectIntHashMap<E>();
    }

    /**
     * Clear datastructures for safe reuses
     */
    public void clear() {
        this.size = 0;
        this.nbLeft = 0;
        this.indices.clear();
    }


    /**
     * Swaps two elements in the list containing all the objects of the set.
     */

    private void swap(int idx1, int idx2) {
        if (idx1 != idx2) {
            E obj1 = objects[idx1];
            E obj2 = objects[idx2];
            this.objects[idx1] =  obj2;
            this.objects[idx2] = obj1;
            this.indices.put(obj1, idx2);
            this.indices.put(obj2, idx1);
        }
    }


    /**
     * Moves the object the left part of the set if needed.
     */

    public void moveLeft(E object) {
        if (!indices.contains(object)) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.logp(Level.SEVERE, "BipartiteSet", "moveLeft", "bipartite set does not contain " + object);
            }
        } else {
            int idx = indices.get(object);
            if (idx >= this.nbLeft) {
                swap(idx, this.nbLeft++);
            }
        }
    }


    /**
     * Moves the object the right part of the set if needed.
     */

    public void moveRight(E object) {
        if (!indices.contains(object)) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.logp(Level.SEVERE, "BipartiteSet", "moveRight", "bipartite set does not contain " + object);
            }
        } else {
            int idx = indices.get(object);
            if (idx < this.nbLeft) {
                swap(idx, --this.nbLeft);
            }
        }
    }


    /**
     * Moves all the objects to the left part.
     */

    public void moveAllLeft() {
        this.nbLeft = this.size;
    }


    /**
     * Moves all the objects to the right part.
     */

    public void moveAllRight() {
        this.nbLeft = 0;
    }


    /**
     * Adds an object to the right part of the set.
     */

    public void addRight(E object) {
        if (this.indices.containsKey(object)) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.logp(Level.SEVERE, "BipartiteSet", "addRight", object + "already in the set bipartite set ");
            }
        } else {
            ensureCapacity(size+1);
            objects[size++] = object;
            indices.put(object, size - 1);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void ensureCapacity(int newSize) {
        if(newSize>objects.length){
            E[] tmp = objects;
            objects = (E[]) new Object[size * 3/2 + 1];
            System.arraycopy(tmp, 0, objects, 0, size);
        }
    }


    /**
     * Adds an object to the left part of the set.
     */

    public void addLeft(E object) {
        this.addRight(object);
        this.moveLeft(object);
    }


    /**
     * Checks if the object is in the left part of the set.
     */

    public boolean isLeft(E object) {
        if (!indices.contains(object)) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.logp(Level.SEVERE, "BipartiteSet", "isLeft", "bipartite set does not contain " + object);
            }
            return false;
        } else {
            return (indices.get(object) < this.nbLeft);
        }
    }


    /**
     * Checks if the object is in the set.
     */

    public boolean isIn(E object) {
        return (this.indices.containsKey(object));
    }


    /**
     * Returns the number of elements in the left part.
     */

    public int getNbLeft() {
        return this.nbLeft;
    }


    /**
     * Returns the number of elements in the right part.
     */

    public int getNbRight() {
        return (this.size - this.nbLeft);
    }


    /**
     * Returns the number of objects in the set.
     */

    public int getNbObjects() {
        return this.size;
    }


    /**
     * Move the last element in the left part to the right part.
     *
     * @return The moved element.
     */

    public E moveLastLeft() {
        // Autant eviter d'appeler la fonction de hachage pour popper le
        // dernier evenement !
        if (this.nbLeft > 0) {
            return this.objects[--this.nbLeft];
        } else {
            return null;
        }
    }


    /**
     * Iterator without a valid remove method !
     * Warning : suppose the set is not modified suring iterating !
     */

    public Iterator<E> leftIterator() {
        return new LeftItr();
    }

    private final class LeftItr implements Iterator<E> {
        private int cursor = 0;

        public boolean hasNext() {
            return cursor != nbLeft;
        }

        public E next() {
            return objects[cursor++];
        }

        public void remove() {
        }
    }


    /**
     * Iterator without a valid remove method !
     * Warning : suppose the set is not modified suring iterating !
     */

    public Iterator<E> rightIterator() {
        return new RightItr();
    }

    private final class RightItr implements Iterator<E> {
        private int cursor = nbLeft;

        public boolean hasNext() {
            return cursor != size;
        }

        public E next() {
            return objects[cursor++];
        }

        public void remove() {
        }
    }
}
