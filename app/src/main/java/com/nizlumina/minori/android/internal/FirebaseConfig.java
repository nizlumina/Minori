/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.internal;

import java.util.Calendar;

/**
 * Basic config class for Minori Firebase endpoint
 * Set up your own Firebase stuffs. Take a look at the Syncmaru repo to see the database creation process.
 */
public class FirebaseConfig
{
    public static final String FIREBASE_ENDPOINT = "https://minori.firebaseio.com";
    public static final String INDEX_ENDPOINT = FIREBASE_ENDPOINT + "/seasons.json";
    public static final String SEASON_ENDPOINT = FIREBASE_ENDPOINT + "/anime";
    public static final String INDEX_CACHEFILE = "season_index";
    public static final int staleThreshold = 30;
    public static final int staleTimeUnit = Calendar.DAY_OF_YEAR;
}
