import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static ru.serge2nd.octopussy.AppContracts.DATA_KITS
import static ru.serge2nd.octopussy.AppContracts.QUERY
import static ru.serge2nd.octopussy.AppContractsTesting.ID
import static ru.serge2nd.octopussy.AppContractsTesting.K1
import static ru.serge2nd.octopussy.AppContractsTesting.K2
import static ru.serge2nd.octopussy.AppContractsTesting.RS
import static ru.serge2nd.octopussy.AppContractsTesting.Q
import static ru.serge2nd.octopussy.AppContractsTesting.V1
import static ru.serge2nd.octopussy.AppContractsTesting.V2
import static ru.serge2nd.octopussy.AppContractsTesting.contract

[
    contract('evaluateQuery') {
        $"POST /$DATA_KITS/$ID/$QUERY", {
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
        OK {
            body RS
        }
    },

    contract('evaluateQueryWrong') {
        $"POST /$DATA_KITS/$ID/$QUERY", {
            body {}
        }
        BAD_REQUEST {
            body(
                method  : 'POST',
                url     : regex(".*/$DATA_KITS/$ID/$QUERY"),
                status  : SC_BAD_REQUEST.title,
                code    : 'NOT_VALID:queries',
                messages: ['err0']
            )
            notBlankMessages()
        }
    }
]
