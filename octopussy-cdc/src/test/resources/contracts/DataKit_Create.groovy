import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static ru.serge2nd.octopussy.AppContracts.DATA_KITS
import static ru.serge2nd.octopussy.AppContractsTesting.ID
import static ru.serge2nd.octopussy.AppContractsTesting.ID2
import static ru.serge2nd.octopussy.AppContractsTesting.contract

[
    contract('createDataKit') {
        $"POST /$DATA_KITS", {
            body(
                kitId: ID,
                properties: [
                    url  : 'jdbc:h2:mem:db1000',
                    login: 'serge'
                ]
            )
        }
        CREATED {
            body(
                kitId: ID2,
                properties: [
                    abc: 'xyz'
                ]
            )
        }
    },

    contract('createExistingDataKit') {
        $"POST /$DATA_KITS", {
            body(
                kitId: ID2,
                properties: [
                    abc: 'xyz'
                ]
            )
        }
        BAD_REQUEST {
            body(
                method  : 'POST',
                url     : regex(".*/$DATA_KITS"),
                status  : SC_BAD_REQUEST.title,
                code    : "DATA_KIT_EXISTS:$ID2",
                messages: ['err0']
            )
            notBlankMessages()
        }
    },

    contract('createInvalidDataKit') {
        $"POST /$DATA_KITS", {
            body {}
        }
        BAD_REQUEST {
            body(
                method  : 'POST',
                url     : regex(".*/$DATA_KITS"),
                status  : SC_BAD_REQUEST.title,
                code    : 'NOT_VALID:dataKitDefinition',
                messages: ['err0']
            )
            notBlankMessages()
        }
    }
]
