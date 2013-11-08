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

package choco.kernel.common.util.bitmask;

import java.util.List;

public final class BitMask implements IBitMask {

	private long bitMask;

	public BitMask() {
		super();
		bitMask = 0;
	}
	
	/**
	 * copy constructor.
	 */
	public BitMask(BitMask toCopy) {
		super();
		this.bitMask = toCopy.bitMask;
	}

	
	@Override
	public long getBitMask() {
		return bitMask;
	}

	public void clear() {
		bitMask = 0;
	}
	
	public boolean isEmpty() {
		return bitMask == 0;
	}

	public boolean contains(final long mask) {
		return (bitMask & mask) == mask;
	}
	
	public boolean or(final IBitMask... settings) {
		for (IBitMask setting : settings) {
			if(contains(setting)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(final IBitMask setting) {
		return contains(setting.getBitMask());
	}

	
	public void set(final long mask) {
		bitMask |= mask;		
	}

	public void toggle(final long mask) {
		bitMask ^= mask;
	}

	public void unset(final long mask) {
		bitMask &= ~mask;
	}

	public void read(List<String> options, StringMask... settings) {
		for (StringMask setting : settings) {
			if(options.contains(setting.getOption())) {
				set(setting);
			}
		}
	}
	
	public void unset(IBitMask... settings) {
		for (IBitMask setting : settings) {
			unset(setting.getBitMask());
		}
	}
	
	public void set(IBitMask... settings) {
		for (IBitMask setting : settings) {
			set(setting.getBitMask());
		}
	}


	@Override
	public String toString() {
		return String.valueOf(bitMask);
	}
}
