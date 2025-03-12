// noinspection JSUnusedGlobalSymbols,ES6ConvertVarToLetConst
// noinspection ES6ConvertVarToLetConst
var request = {
    variables: {
        dataHolder: {},
        get: function (key) {
            return this.dataHolder[key] !== undefined ? this.dataHolder[key] : null;
        },
        set: function (key, val) {
            var desc
            if (val !== null) {
                val = val + '';
                if (val.length > 100) {
                    desc = val.substring(0, 100) + "...(已截断显示)"
                } else {
                    desc = val;
                }
            } else {
                desc = "null";
            }

            this.dataHolder[key] = val;
            client.log(key + ' 已设置为(request): ' + desc);
        },
    }
};