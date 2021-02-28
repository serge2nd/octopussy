import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static ru.serge2nd.octopussy.AppContracts.DATA_KITS
import static ru.serge2nd.octopussy.AppContractsTesting.ID
import static ru.serge2nd.octopussy.AppContractsTesting.ID2
import static ru.serge2nd.octopussy.AppContractsTesting.contract

[
    contract('getAllDataKits') {
        $"GET /$DATA_KITS", {}
        '200 OK' {
            body([[
                kitId: ID,
                properties: [
                    url: 'jdbc:h2:mem:db1000',
                    login: 'serge'
                ]
            ], [
                kitId: ID2,
                properties: [
                    abc: 'xyz'
                ]
            ]])
        }
    },

    contract('getDataKit') {
        $"GET /$DATA_KITS/$ID", {}
        '200 OK' {
            body(
                kitId: ID,
                properties: [
                    url: 'jdbc:h2:mem:db1000',
                    login: 'serge'
                ]
            )
        }
    },

    contract('getDataKitNotFound') {
        $"GET /$DATA_KITS/$ID2", {}
        '404 Not Found' {
            body(
                method : 'GET',
                url    : regex(".*/$DATA_KITS/$ID2"),
                status : SC_NOT_FOUND.title,
                code   : "DATA_KIT_NOT_FOUND:$ID2",
                messages: ['err0']
            )
            notBlankMessages()
        }
    }
]
