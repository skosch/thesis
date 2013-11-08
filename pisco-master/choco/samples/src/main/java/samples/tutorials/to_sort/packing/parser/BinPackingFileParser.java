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
package samples.tutorials.to_sort.packing.parser;

import parser.instances.InstanceFileParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 8 juil. 2010
 */
final class BinPackingFileParser implements InstanceFileParser {

    public File file;
    public int capacity;
    public int[] sizes;

    @Override
    public File getInstanceFile() {
        return file;
    }

    @Override
    public void loadInstance(File file) {
        this.file = file;
    }

    @Override
    public void parse(boolean displayInstance){
        Scanner sc = null;
        try {
            sc = new Scanner(file);
            int nb = sc.nextInt();
            capacity = sc.nextInt();
            sizes = new int[nb];
            for (int i=0 ; i < nb ; i++) {
                sizes[i] = sc.nextInt();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(displayInstance && LOGGER.isLoggable(Level.INFO)){
            LOGGER.log(Level.INFO, "capacity : {0},\nWeights : {1}.\n",
                    new String[]{Integer.toString(capacity), Arrays.toString(sizes)});
        }
    }
    

    @Override
    public void cleanup() {
        this.file = null;
        this.capacity = 0;
        this.sizes = null;
    }

    public Object getParameters(){
        return new Object[]{sizes, capacity};
    }
}
