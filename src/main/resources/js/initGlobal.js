// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst

// Note that the file cannot access javaBridge objects due to the scope issues
var client = {
    log: function (args) {
        const strList = [];
        const length = arguments.length;
        for (var i = 0; i < length; i++) {
            const arg = arguments[i];
            if ( typeof arg === 'object') {
                try {
                    strList.push(JSON.stringify(arg));
                } catch (e) {
                    strList.push(`${arg}`);
                }
            } else {
                strList.push(`${arg}`);
            }
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
    test: function (testName, assertCallback) {
        assertCallback();
        globalLog.log(testName);
    },
    assert: function (condition, message) {
        if (!condition) {
            throw new Error(message);
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
            const arg = arguments[i];
            if (typeof arg === 'object') {
                try {
                    strList.push(JSON.stringify(arg));
                } catch (e) {
                    strList.push(`${arg}`);
                }
            } else {
                strList.push(`${arg}`);
            }
        }
        globalLog.log(strList.join(" "));
    },
}

function headersAll(headers) {
    return Object.keys(headers)
        .filter(key => typeof headers[key] !== 'function')
        .map(key => {
            const requestHeader = {
                name: key,
                values: headers[key]
            };
            requestHeader.getRawValue = function () {
                return headers[key][0] + "";
            }
            requestHeader.tryGetSubstitutedValue = function () {
                return headers[key][0] + "";
            }
            requestHeader.value = function () {
                return headers[key][0] + "";
            }
            return requestHeader
        })
}

function headersFindByName(headers, name) {
    const requestHeaders = headersFindListByName(headers, name);
    if (requestHeaders.length === 0) {
        return null;
    }

    return requestHeaders[0];
}

function headersFindValuesByName(headers, name) {
    const requestHeader = headersFindByName(headers, name);
    if (requestHeader == null) {
        return [];
    }

    return requestHeader.values;
}

function headersFindFirstValueByName(headers, name) {
    const headerValues = headersFindValuesByName(headers, name);
    if (headerValues.length === 0) {
        return null;
    }

    return headerValues[0];
}

function headersFindListByName(headers, name) {
    return headersAll(headers)
        .filter(it => it.name.toLowerCase() === name.toLowerCase());
}

function resolveContentType(headers) {
    const value = headersFindFirstValueByName(headers, 'Content-Type');
    const split = value.split(';');
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

URLSearchParams.prototype.keys = function () {
    return Object.keys(this.params);
};

URLSearchParams.prototype.values = function () {
    return Object.values(this.params);
};

URLSearchParams.prototype.entries = function () {
    return Object.entries(this.params);
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
