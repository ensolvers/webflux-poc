# Autoformatting hooks for frontend via Husky

By following the steps below you can configure [Husky](https://www.npmjs.com/package/husky) to run code autoformatting before each commit

## Install dependencies

```
npm install husky@4 lint-staged --save-dev --save-exact
npm install prettier --save-dev --save-exact
```

## Add the following to package.json

```
  "husky": {
    "hooks": {
      "pre-commit": "lint-staged"
    }
  },
  "lint-staged": {
    "src/**/*.{js,jsx,ts,tsx,json,css,scss,md}": [
      "prettier --write"
    ]
  }
```
