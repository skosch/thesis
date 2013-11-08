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

package parser.instances.checker;


import static java.lang.Integer.parseInt;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import parser.instances.AbstractInstanceModel;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.PropertyUtils;

/**
 * Static storage of properties that represents the best-known status of some instances.
 * Properties are given as follows: 
 * <ul>
 * <li> instName=true/false (case insensitive) #CSP</li>
 * <li> instName=(OPT|LB:UB) #Optimization (LB<=UB)</li>
 * </ul>
 * 
 * @author Arnaud Malapert</br> 
 * @since 19 févr. 2010 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public final class SCheckFactory {

	public static final Logger LOGGER = ChocoLogging.getMainLogger();

	public static final Properties PROPERTIES = new Properties();


	public static void load(File... files) {
		PropertyUtils.loadProperties(PROPERTIES, files);
	}

	public static void load(String... resources) {
		PropertyUtils.loadProperties(PROPERTIES, resources);
	}

	public static IStatusChecker makeStatusChecker(AbstractInstanceModel model) {
		return makeStatusChecker(model.getInstanceName());
	}

	
	public static IStatusChecker makeStatusChecker(String key) {
		String pvalue = SCheckFactory.PROPERTIES.getProperty(key);
		if( pvalue != null) {
			pvalue = pvalue.trim();
			try {
				final int splitIndex = pvalue.indexOf(':'); 
				if(splitIndex == -1) {
					if(pvalue.equalsIgnoreCase("true")) return new SatSChecker(true);
					else if (pvalue.equalsIgnoreCase("false")) return new SatSChecker(false);
					else return new OptimSChecker(parseInt(pvalue)); //format OPT
				}else {
					//format LB:UB 
					return new OptimSChecker(
							parseInt(pvalue.substring(0, splitIndex)),
							parseInt(pvalue.substring(splitIndex+1))
					);
				}
			} catch (NumberFormatException e) {
				SCheckFactory.LOGGER.log(Level.SEVERE,"properties...[invalid-checker-format][{0}]",key);
				return null;
			}	
		}
		SCheckFactory.LOGGER.log(Level.CONFIG,"properties...[no-status-checker][{0}]",key);
		return null;
	}
}
