#Minori  
Minori is an Android app that allows you to follow releases on a certain cat site.  
It can be paired with any torrent app that can watch a directory for a full automation.


##Core packages  
Currently the classes under this package use Hummingbird API v1 model and a standard model of a Nyaa XML entry.

##Season database
To relief the strain on various servers that provided this data, the season database currently runs on our own Firebase instance. This database is updated monthly on best-effort/new-info basis. The `CompositeData` model applicable to this linkage is provided under the Syncmaru package and described below:

- `MALObject`: A POJO for the official MAL API
- `SmallAnimeObject`: A sparse version derived from Hummingbird API v2 AnimeObject
- `LiveChartObject`: A parsed version of a LiveChart card

(We did created a working Anichart model by mistake. However, we still prefer LiveChart for its ease of use and opted to leave the Anichart stuffs in Syncmaru as backup).

Feel free to check the Syncmaru console application for the relevant CRUD tasks [here](https://github.com/nizlumina/Syncmaru). 

##Building
Clone the repo via Git and build.

OR 

Just use the *"Checkout project from Version Control"* option when starting Android Studio and in the *Vcs Repository Url* section, insert the URL:

https://github.com/nizlumina/Minori.git

and build.

##Dependencies
The project make use of the following great libraries:

- [Glide](https://github.com/bumptech/glide)
- [OkHttp](https://github.com/square/okhttp)
- [GSON](http://code.google.com/p/google-gson/)
- [Commons IO](http://commons.apache.org/proper/commons-io/)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)

##License
Where applicable, this Minori project follows the standard MIT license as below:

The MIT License (MIT)

Copyright (c) 2014 Nizlumina Studio (Malaysia)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

