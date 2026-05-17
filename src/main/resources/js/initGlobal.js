// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst,JSUnresolvedReference
// noinspection ES6ConvertVarToLetConst

// Note that the file cannot access javaBridge objects due to the scope issues
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