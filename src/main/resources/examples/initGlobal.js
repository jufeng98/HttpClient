// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst
var client = {
    log: function (msgs) {
        const strList = [];
        const length = arguments.length;
        for (var i = 0; i < length; i++) {
            strList.push(`${arguments[i]}`)
        }
        globalLog.log(strList.join(" "));
    },
    global: {
        dataHolder: {},
        isEmpty: function () {
            return Object.keys(this.dataHolder).length === 0;
        },
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
        },
        clear: function (key) {
            return delete this.dataHolder[key];
        },
        clearAll: function () {
            this.dataHolder = {};
            return true;
        },
        set: function (key, val) {
            var desc
            if (val !== null) {
                val = val + '';
                if (val.length > 300) {
                    desc = val.substring(0, 300) + "...(已截断显示)"
                } else {
                    desc = val;
                }
            } else {
                desc = "null";
            }

            this.dataHolder[key] = val;
            globalLog.log(key + ' 已设置为(global): ' + desc);
        },
    },
    test: function (successMsg, assertCallback) {
        try {
            assertCallback();
            globalLog.log(successMsg);
        } catch (e) {
            globalLog.log(e);
        }
    },
    assert: function (success, failMsg) {
        if (!success) {
            throw new Error(failMsg);
        }
    }
};

var console = {
    log: function (msgs) {
        const strList = [];
        const length = arguments.length;
        for (var i = 0; i < length; i++) {
            strList.push(`${arguments[i]}`)
        }
        globalLog.log(strList.join(" "));
    },
}

function hasGlobalVariableKey(key) {
    return Object.keys(client.global.dataHolder).indexOf(key) !== -1;
}

function getGlobalVariable(key) {
    return client.global.get(key);
}

function URLSearchParams(queryParams) {
    if (queryParams === null || queryParams === undefined || queryParams === '') {
        this.params = {};
        return;
    }

    this.params = parseQueryParams(queryParams);

    function parseQueryParams(queryParams) {
        return queryParams.split('&')
            .reduce((params, pair) => {
                const [key, value] = pair.split('=');
                params[decodeURIComponent(key)] = decodeURIComponent(value || '');
                return params;
            }, {});
    }
}

URLSearchParams.prototype.append = function (key, value) {
    this.params[key] = value;
};

URLSearchParams.prototype.has = function (key) {
    return this.params[key] !== undefined;
};

URLSearchParams.prototype.get = function (key) {
    return this.params[key];
};

URLSearchParams.prototype.set = function (key, value) {
    this.params[key] = value;
};

URLSearchParams.prototype.delete = function (key) {
    delete this.params[key];
};

URLSearchParams.prototype.toString = function () {
    const strList = [];
    const keys = Object.keys(this.params);
    for (var i = 0; i < keys.length; i++) {
        var key = keys[i];
        strList.push(encodeURIComponent(key) + "=" + encodeURIComponent(this.params[key]));
    }
    return strList.join("&");
};
