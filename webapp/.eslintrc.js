module.exports = {
    "env": {
        "browser": true,
        "commonjs": true,
        "es6": true
    },
    "extends": "eslint:recommended",
    "parserOptions": {
        "ecmaFeatures": {
            "experimentalObjectRestSpread": true,
            "jsx": true
        },
        "sourceType": "module"
    },
    "plugins": [
        "react"
    ],
    "rules": {
        "indent": [
            "warn",
            2,
            { "SwitchCase": 1 }
        ],
        "linebreak-style": [
            "warn",
            "unix"
        ],
        "semi": [
            "error",
            "always"
        ],
        "no-console": [
            "warn", {"allow": ["assert", "warn", "error"]}
        ],
        "no-unused-vars": [
            "warn", {
              "args": "none",
              "varsIgnorePattern": '(React)'
            }
        ],
        "react/jsx-uses-vars": 2,
        "no-class-assign": 0, /* allow higher-order-components */
    }
};
