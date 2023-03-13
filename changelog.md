# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased
### Changed
- Update plugins and dependencies

## 3.0.5 - 2023-02-11
## Changed
- Downgrade slf4j-api to 2.0.5 because of javadocs

## 3.0.4 - 2023-02-07 - not published
### Fixed
- Update dependencies.
- Properties throws NPE if the lambda-logger.properties is missed then initialisation fails.

## 3.0.3 - 2022-09-10
- Update dependencies
- Remove all static binders

## 3.0.2 - 2022-08-22
This version is not released, just update documentation.

- Fix documentation's issue

## 3.0.1 - 2022-08-22
This version is not released, just update documentation.

- Update the [example](example)
- Update documentation
- Update dependencies

## 3.0.0 - 2022-08-21
- **Add a module**
- Update dependencies
- Update documentation

## 2.1.0 - 2022-08-01
### Changed
- Custom level and marker separators
- Update example

## 2.0.0 - 2022-08-01
### Added
- Support Markers
- Update dependencies
- Update javadocs and site
- Multi-level and multi-marker configuration

## 1.1.0 - 2022-06-29
### Added
- Name of AWS request ID is configurable
### Fixed
- CRLF injection issue #2

## 1.0.1 - 2022-05-06
### Changed
- Publish to Maven Central instead of GitLab.

## 1.0.0 - 2022-05-04
### Added
- Implement a logger.
- Add Javadocs.
- Add the site.
