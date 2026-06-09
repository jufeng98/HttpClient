// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference,ThisExpressionReferencesGlobalObjectJS
// noinspection ES6ConvertVarToLetConst
defileProperty('$historyFolder');
defileProperty('$isoTimestamp');
defileProperty('$projectRoot');
defileProperty('$historyFolder');
defileProperty('$mvnTarget');
defileProperty('$randomInt')
defileProperty('$timestamp')
defileProperty('$uuid')
defileProperty('$datetime')

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
    double(from, to) {
        return javaBridge.callJava('$random.double', from, to);
    },
    hexadecimal(length) {
        return javaBridge.callJava('$random.hexadecimal', length);
    },
    integer(from, to) {
        return javaBridge.callJava('$random.integer', from, to);
    },
    address: {}, ancient: {}, beer: {}, bool: {}, book: {}, business: {},
    chuckNorris: {}, code: {}, color: {}, commerce: {}, company: {}, crypto: {}, dateAndTime: {},
    educator: {}, finance: {}, hacker: {}, idNumber: {}, lorem: {}, name: {}, number: {},
    app: {}, options: {}, phoneNumber: {}, shakespeare: {}, superhero: {}, nation: {},
    university: {}, internet: {}, animal: {}, team: {}, programmingLanguage: {},
};

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
registerFakerFields('com.github.javafaker.App', 'app');
registerFakerFields('com.github.javafaker.Number', 'number');
registerFakerFields('com.github.javafaker.PhoneNumber', 'phoneNumber');
registerFakerFields('com.github.javafaker.Shakespeare', 'shakespeare');
registerFakerFields('com.github.javafaker.Superhero', 'superhero');
registerFakerFields('com.github.javafaker.Nation', 'nation');
registerFakerFields('com.github.javafaker.University', 'university');
registerFakerFields('com.github.javafaker.Internet', 'internet');
registerFakerFields('com.github.javafaker.Animal', 'animal');
registerFakerFields('com.github.javafaker.Team', 'team');
registerFakerFields('com.github.javafaker.ProgrammingLanguage', 'programmingLanguage');

function registerFakerFields(clzName, suffix) {
    javaBridge.getClassNoArgDeclareMethodNames(clzName)
        .forEach(it => {
            Object.defineProperty($random[suffix], it, {
                get() {
                    return javaBridge.callJava('$random.' + suffix, it);
                },
                enumerable: true,
                configurable: true
            });
        });
}

function defileProperty(name) {
    Object.defineProperty(this, name, {
        get() {
            return javaBridge.callJava(name);
        },
        enumerable: true,
        configurable: true
    });
}