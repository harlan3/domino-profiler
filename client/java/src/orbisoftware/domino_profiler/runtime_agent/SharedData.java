/*
	MIT License
	
	Copyright (c) Harlan Murphy
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
 */
package orbisoftware.domino_profiler.runtime_agent;

import java.util.HashMap;

import org.json.JSONObject;

public class SharedData {

	public JSONObject jsonObject = new JSONObject();

	public HashMap<String, String> xmlMap;

	private static SharedData instance = null;

	private SharedData() {

		xmlMap = new HashMap<>();

		// Load default record sets and number of keys for the JSON objects
		jsonObject.put("json_intrinsics.recordSet", Short.valueOf((short) 1));
		jsonObject.put("json_intrinsics.numberKeys", Short.valueOf((short) 2));
	}

	public static SharedData getInstance() {
		
		if (instance == null) {
			instance = new SharedData();
		}
		return instance;
	}
}
