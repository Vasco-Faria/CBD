
FuncC = function () {

    return db.phones.aggregate([{$group: {_id: "$components.prefix", numPhones: {$sum: 1}}}])
}