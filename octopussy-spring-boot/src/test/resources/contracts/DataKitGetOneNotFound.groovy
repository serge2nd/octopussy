import org.springframework.cloud.contract.spec.Contract
import static org.springframework.cloud.contract.spec.internal.HttpMethods.GET
import static org.springframework.http.HttpStatus.NOT_FOUND

Contract.make {
    description "must provide error info if data kit not found"
    request {
        method GET
        url '/dataKits/db3000'
    }
    response {
        status NOT_FOUND.value()
        body(
            url    : regex('.*/dataKits/db3000'),
            method : GET,
            status : "${NOT_FOUND.value()} ${NOT_FOUND.reasonPhrase}" as String,
            code   : 'DATA_KIT_NOT_FOUND:db3000',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}
