// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst

// Note that the file cannot access javaBridge objects due to the scope issues
var client = {
    log: function (args) {
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
            globalLog.log(key + ` ${GLOBAL_SET} ` + desc);
        },
    },
    test: function (successMsg, assertCallback) {
        assertCallback();
        globalLog.log(successMsg);
    },
    assert: function (success, failMsg) {
        if (!success) {
            throw new Error(failMsg);
        }
    },
    exit: function () {
        throw new Error("Exit");
    }
};

var console = {
    log: function (args) {
        const strList = [];
        const length = arguments.length;
        for (var i = 0; i < length; i++) {
            strList.push(`${arguments[i]}`)
        }
        globalLog.log(strList.join(" "));
    },
}

function headersAll(headers) {
    return Object.keys(headers)
        .map(key => {
            return {
                name: key,
                values: headers[key]
            }
        })
}

function headersFindByName(headers, name) {
    const values = headersFindListByName(headers, name);
    if (values === null || values.length === 0) {
        return null;
    }

    return values[0];
}

function headersFindListByName(headers, name) {
    if (!name) {
        return headersAll(headers, name);
    }

    const list = headersAll(headers).filter(it => it.name.toLowerCase() === name.toLowerCase());
    if (list.length === 0) {
        return null;
    }

    return list[0].values;
}

function resolveContentType(headers) {
    const value = headersFindByName(headers, 'Content-Type');
    if (!value) {
        return null;
    }

    const split = value.split(';')
    return {
        mimeType: (split[0] || '').trim(),
        charset: (split[1] || '').trim()
    }
}

function hasGlobalVariableKey(key) {
    return Object.keys(client.global.dataHolder).indexOf(key) !== -1;
}

function getGlobalVariable(key) {
    return client.global.get(key);
}

function URLSearchParams(queryParams) {
    this.params = parseQueryParams(queryParams);

    Object.defineProperty(this, 'size', {
        get() {
            return Object.keys(this.params).length;
        }
    });

    function parseQueryParams(queryParams) {
        if (queryParams === null || queryParams === undefined || queryParams === '') {
            return {};
        }

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
