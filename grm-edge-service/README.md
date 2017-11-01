
# Build
Jenkins Job: 

To Build you must do the following.
1. Tag the commit with a version that we will use to tag the new build with. For instance (1.1.2) is a proper tag
2. Navigate to the Jenkins job listed above.
3. Select "Build with Paramaters" and select the tag created in #2.
4. Leave Tag version checked, this will tag the build image with the version you specified in your git tag.
5. Leave tag latest unchecked unless you want this to override the latest build. This should only be checked if it is a certified version.