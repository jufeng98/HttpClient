// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference,ThisExpressionReferencesGlobalObjectJS
// noinspection ES6ConvertVarToLetConst
var request = {
    variables: {
        dataHolder: {},
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
        },
        isEmpty: function () {
            return Object.keys(this.dataHolder).length === 0;
        },
        clear: function (key) {
            delete this.dataHolder[key];
        },
        clearAll: function () {
            this.dataHolder = {};
        },
        set: function (key, val) {
            var desc
            if (val !== null) {
                val = val + '';
                if (val.length > 300) {
                    desc = val.substring(0, 300) + `...(${CONTENT_TRUNCATED})`
                } else {
                    desc = val;
                }
            } else {
                desc = "null";
            }

            this.dataHolder[key] = val;
            globalLog.log(key + ` ${REQUEST_SET} ` + desc);
        },
    }
};

Object.defineProperty(this, '$isoTimestamp', {
    get() {
        return javaBridge.callJava('$isoTimestamp');
    },
    enumerable: true,
    configurable: true
});

Object.defineProperty(this, '$projectRoot', {
    get() {
        return javaBridge.callJava('$projectRoot');
    },
    enumerable: true,
    configurable: true
});

var $random = {
    alphabetic(length) {
        return javaBridge.callJava('$random.alphabetic', length);
    },
    alphanumeric(length) {
        return javaBridge.callJava('$random.alphanumeric', length);
    },
    float(from, to) {
        return javaBridge.callJava('$random.float', from, to);
    },
    hexadecimal(length) {
        return javaBridge.callJava('$random.hexadecimal', length);
    },
    integer(from, to) {
        return javaBridge.callJava('$random.integer', from, to);
    },
    address: {},
    ancient: {},
    beer: {},
    bool: {},
    book: {},
    business: {},
    chuckNorris: {},
    code: {},
    color: {},
    commerce: {},
    company: {},
    crypto: {},
    dateAndTime: {},
    educator: {},
    finance: {},
    hacker: {},
    idNumber: {},
    lorem: {},
    name: {},
    number: {},
    options: {},
    phoneNumber: {},
    shakespeare: {},
    superhero: {},
    team: {},
    university: {},
};

registerFakerFields('com.github.javafaker.Address', 'address');
registerFakerFields('com.github.javafaker.Ancient', 'ancient');
registerFakerFields('com.github.javafaker.Beer', 'beer');
registerFakerFields('com.github.javafaker.Bool', 'bool');
registerFakerFields('com.github.javafaker.Book', 'book');
registerFakerFields('com.github.javafaker.Business', 'business');
registerFakerFields('com.github.javafaker.ChuckNorris', 'chuckNorris');
registerFakerFields('com.github.javafaker.Code', 'code');
registerFakerFields('com.github.javafaker.Color', 'color');
registerFakerFields('com.github.javafaker.Commerce', 'commerce');
registerFakerFields('com.github.javafaker.Company', 'company');
registerFakerFields('com.github.javafaker.Crypto', 'crypto');
registerFakerFields('com.github.javafaker.DateAndTime', 'dateAndTime');
registerFakerFields('com.github.javafaker.Educator', 'educator');
registerFakerFields('com.github.javafaker.Finance', 'finance');
registerFakerFields('com.github.javafaker.Hacker', 'hacker');
registerFakerFields('com.github.javafaker.IdNumber', 'idNumber');
registerFakerFields('com.github.javafaker.Internet', 'internet');
registerFakerFields('com.github.javafaker.Lorem', 'lorem');
registerFakerFields('com.github.javafaker.Name', 'name');
registerFakerFields('com.github.javafaker.Number', 'number');
registerFakerFields('com.github.javafaker.PhoneNumber', 'phoneNumber');
registerFakerFields('com.github.javafaker.Shakespeare', 'shakespeare');
registerFakerFields('com.github.javafaker.Superhero', 'superhero');
registerFakerFields('com.github.javafaker.Team', 'team');
registerFakerFields('com.github.javafaker.University', 'university');

Object.defineProperty($random, 'email', {
    get() {
        return javaBridge.callJava('$random.email');
    },
    enumerable: true,
    configurable: true
});

Object.defineProperty($random, 'uuid', {
    get() {
        return javaBridge.callJava('$random.uuid');
    },
    enumerable: true,
    configurable: true
});

var Window = {
    btoa: function btoa(bytes) {
        return javaBridge.btoa(bytes);
    },
    atob: function atob(str) {
        return javaBridge.atob(str);
    }
}

function registerFakerFields(clzName, suffix) {
    javaBridge.getClassNoArgDeclareMethodNames(clzName)
        .forEach(it => {
            Object.defineProperty($random.address, it, {
                get() {
                    return javaBridge.callJava('$random.' + suffix, it);
                },
                enumerable: true,
                configurable: true
            });
        });
}

function hasRequestVariableKey(key) {
    return Object.keys(request.variables.dataHolder).indexOf(key) !== -1;
}

function getRequestVariable(key) {
    return request.variables.get(key);
}

function base64ToFile(base64, path) {
    javaBridge.base64ToFile(base64, path);
}

function jsonPath(obj, expression) {
    return javaBridge.jsonPath(obj, expression);
}

function xpath(obj, expression) {
    return javaBridge.xpath(obj, expression);
}