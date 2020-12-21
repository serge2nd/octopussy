import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.http.HttpStatus

import static org.springframework.cloud.contract.spec.internal.HttpMethods.DELETE
Integer.metaClass.title = { delegate + ' ' + HttpStatus.resolve(delegate).reasonPhrase }

[Contract.make {
    name 'deleteDataKit'
    'DELETE ∕dataKits∕'('db1000') {}
    response {
        status NO_CONTENT()
        body ''
    }
},
Contract.make {
    name 'deleteDataKitNotFound'
    'DELETE ∕dataKits∕'('db3000') {}
    response {
        status NOT_FOUND()
        body(
            url    : regex('.*/dataKits/db3000'),
            method : DELETE,
            status : NOT_FOUND().title(),
            code   : 'DATA_KIT_NOT_FOUND:db3000',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}]

static 'DELETE ∕dataKits∕'(String id, @DelegatesTo(Request) Closure $$) {
    $$.owner.delegate.request $$

    $$.delegate.method DELETE
    $$.delegate.url    "/dataKits/$id"
}