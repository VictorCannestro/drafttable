# Contributing

We welcome contributions! Suggestions for improvements and refactorings are welcome and encouraged. If you have an idea
or suggestion, please raise an [Issue](https://github.com/VictorCannestro/drafttable/issues), email, or any other method with the owners of this repository before
attempting to make a change.

Please note we have a [code of conduct](CODE_OF_CONDUCT.md). Be sure to follow it in all your interactions with the project.

## Development and Release
Development happens off of the `develop` branch, which has the version number of the next release with "-SNAPSHOT" 
appended to it in the `gradle.properties` file. All `master` branch releases will drop the  "-SNAPSHOT" post-fix in the
version. The versioning scheme we use is [SemVer](http://semver.org/).

### Testing
Automated testing for any changes is important for preventing accidental breakage in the future. Tests also document and
demonstrate the bounds of functionality, showing the author's intent to others working on the code in the future. As 
such, it's imperative that new any new code changes come with associated tests. 

## Pull Request Process

1. Ensure any install or build dependencies are removed before the end of the layer when doing a
   build.
2. Update the `README.md` or any other relevant docs with details of changes to public interfaces or APIs.
3. Increase the version numbers in any examples files and the `README.md` to the new version that this
   Pull Request would represent. 
4. You may merge the Pull Request in once you have the sign-off of two other developers, or if you
   do not have permission to do that, you may request the second reviewer to merge it for you.

### Pull Request Template
```
#### Title: 
#### Summary
* Add a summary of why the changes needed to be made here 
* What those changes accomplish?

#### Checklist
This PR:
- [x] Adds new tests
- [x] Adds new features
- [x] Includes refactorings of existing functionality
- [x] Adds/updates documentation 
- [ ] Modifies public-facing APIs
```
