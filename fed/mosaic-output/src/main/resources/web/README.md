# WebSocket Visualizer with OpenLayers

## Requirements

Only NodeJS (https://nodejs.org/en/download/)

## Install Development Requirements

Change to the visualizr directory:  
`cd <MOSAIC_SOURCE>/fed/mosaic-output/src/main/resources/web`

Install required modules for development and deployment (defined in package.json):  
`npm install`

That's it.

## Development

It is recommended to use VSCode (https://code.visualstudio.com/download) for the development of WebVisualizer since it is free and offers a very good supports for easy JS development like linting and suggestions.
If you want to make changes you need to make them in the `visualizer-dev.js`.  
You can install eslint (https://eslint.org/) globally with `npm i -g eslint` to support you to write correct code.

## Building visualizer.js

To deploy your changes you have two possibilites:  
1. Creating a readable version with `npm run-script build`.
2. Creating a non-readable version with `npm run-script buildUgly`. This version is loaded faster by the browser.

To debug your changes create `visualizer.js` with the command `npm run-script debug`.
This will add debugging information to `visualizer.js` which will be parsed by a modern browser,
such that you backtrack error to individual javascript files using the browser's developer tools.
