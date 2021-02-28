import static org.apache.http.HttpStatus.SC_NOT_FOUND
import static ru.serge2nd.octopussy.AppContracts.DATA_KITS
import static ru.serge2nd.octopussy.AppContractsTesting.ID
import static ru.serge2nd.octopussy.AppContractsTesting.ID2
import static ru.serge2nd.octopussy.AppContractsTesting.contract

[
    contract('deleteDataKit') {
        $"DELETE /$DATA_KITS/$ID", {}
        '204 No Content' {}
    },

    contract('deleteDataKitNotFound') {
        $"DELETE /$DATA_KITS/$ID2", {}
        '404 Not Found' {
            body(
                method  : 'DELETE',
                url     : regex(".*/$DATA_KITS/$ID2"),
                status  : SC_NOT_FOUND.title,
                code    : "DATA_KIT_NOT_FOUND:$ID2",
                messages: ['err0']
            )
            notBlankMessages()
        }
    }
]
