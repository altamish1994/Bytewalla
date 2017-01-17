/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */
package se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.storage.thread;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * MsgBlockingQueue class for Thread communication in Android DTN
 *  @author Rerngvit Yanggratoke (rerngvit@kth.se) 
 */
public class MsgBlockingQueue<E> extends ArrayBlockingQueue<E> implements Serializable {

	/**
	 * Constructor by taking capacity as input
	 * @param capacity
	 */
	public MsgBlockingQueue(int capacity) {
		super(capacity);
		
	}

	/**
	 * Constructor by taking both capacity and whether the queue will be fair
	 * @param capacity
	 * @param fair
	 */
	public MsgBlockingQueue(int capacity, boolean fair) {
		super(capacity, fair);
		
	}
	
	
	/**
	 * Serial version UID to support Java Serializable function
	 */
	private static final long serialVersionUID = -7139268179135110061L;

}