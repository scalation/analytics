# ScalaTion Analytics Ontology Library

This project contains an API for utilizing the ScalaTion Analytics Ontology in
both Scala and Java applications.

### Notes

Currently, the API utilizes the [```HermiT```](http://hermit-reasoner.com) reasoner.
However, since our code is written against the [```OWL API```](http://owlapi.sourceforge.net),
any working reasoner that is compatible with ```OWL API``` should work with our
API.

### Requirements

Most requirements are handled by dependency declarations in ```build.sbt``` that
resolve to content hosted by [Maven Central](http://search.maven.org). There is
one dependency that is required that is not hosted there, ScalaTion.

 * Download the latest version of ScalaTion
   [here](http://cobweb.cs.uga.edu/~jam/scalation_1.1.1.tar.gz).
   
 * Extract the archive.
 
 * Change (```cd```) into the directory.
 
 * Inspect the ```build.sbt``` file. If there is no line starting with
   ```organization```, then add the following to that file (and save):

   ```organization := "scalation"```

 * Now, type the following commands to make ScalaTion available to SBT
   projects:

   ```
   $ sbt publishLocal
   ```

 * You should now be able to proceed with the Build Instructions for this
   project.

### Notices

 * The content and opinions expressed on this Web page do not necessarily
   reflect the views of nor are they endorsed by the University of Georgia or
   the University System of Georgia.

