# Give My Secret Frontend

Welcome to GMS frontend code!

## Build
### Production
Production ready bundle can be built by running `ng build` or `npm run build`.

### "Local" production
This configuration can be used to build the application bundle in production mode but connected to backend on http://localhost:8080.

You need to run `npm run buildLocal`.

## Run
The webapp can be started in many ways:
- HTTP or
- HTTPS mode
- Local production mode

### HTTP mode
Run `npm run start` to start the webapp in HTTP mode.

### HTTPS mode
If you run the backend code on HTTPS port, you need to use this mode by running `npm run startHttps`.

### "Local" production
You can run your production ready webapp bundle with `npm run startLocal` after you executed `npm run buildLocal`.

This mode can help to get rid of unnecessary running of an IDE + the web application together when you want to work only on the backend side code but you need a frontend quickly.

#### Prerequisite
In order to run the bundle, you need to install http-server NPM library:
> npm install angular-http-server -g

#### Resources
- [https://www.npmjs.com/package/angular-http-server](https://www.npmjs.com/package/angular-http-server)

## Testing

### Unit tests

> npm run test

### UI Tests (PlayWright)

UI tests will run on localhost:4199.

#### Run
> npm run uiTest

#### Run report

> npm run showUiReport
