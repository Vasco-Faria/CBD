//numeros sem digitos repetidos

FuncD = function () {
    var numeros = db.phones.find({}, { _id: 1 }).limit(50000).toArray();

    const numeroscomdigitosdif = [];

    numeros.forEach(function(item) {
        if (item._id) {
            const idString = item._id.toString();
            const idWithoutPrefix = idString.slice(3); 
            const digitos = idWithoutPrefix.split('');

            const digitosUnicos = new Set(digitos);

            if (digitos.length === digitosUnicos.size) {
                numeroscomdigitosdif.push(item._id);
            }
        }
    });

    return numeroscomdigitosdif;
}




