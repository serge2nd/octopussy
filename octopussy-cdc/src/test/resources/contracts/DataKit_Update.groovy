import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static ru.serge2nd.octopussy.AppContracts.DATA_KITS
import static ru.serge2nd.octopussy.AppContracts.UPDATE
import static ru.serge2nd.octopussy.AppContractsTesting.ID
import static ru.serge2nd.octopussy.AppContractsTesting.K1
import static ru.serge2nd.octopussy.AppContractsTesting.K2
import static ru.serge2nd.octopussy.AppContractsTesting.RS
import static ru.serge2nd.octopussy.AppContractsTesting.Q
import static ru.serge2nd.octopussy.AppContractsTesting.V1
import static ru.serge2nd.octopussy.AppContractsTesting.V2
import static ru.serge2nd.octopussy.AppContractsTesting.contract

[
    contract('executeUpdate') {
        $"POST /$DATA_KITS/$ID/$UPDATE", {
            body(
                "queries": [[
                    "query": Q,
                    "params": [
                        (K1): V1
                    ]
                ]],
                params: [
                    (K2): V2
                ]
            )
        }
        '200 OK' {
            body RS
        }
    },

    contract('executeUpdateWrong') {
        $"POST /$DATA_KITS/$ID/$UPDATE", {
            body {}
        }
        '400 Bad Request' {
            body(
                method  : 'POST',
                url     : regex(".*/$DATA_KITS/$ID/$UPDATE"),
                status  : SC_BAD_REQUEST.title,
                code    : 'NOT_VALID:queriesRq',
                messages: ['err0']
            )
            notBlankMessages()
        }
    }
]
