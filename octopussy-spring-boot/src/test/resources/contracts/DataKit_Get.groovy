import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.http.HttpStatus

import static org.springframework.cloud.contract.spec.internal.HttpMethods.GET
Integer.metaClass.title = { delegate + ' ' + HttpStatus.resolve(delegate).reasonPhrase }

[Contract.make {
    name 'getAllDataKits'
    'GET ∕dataKits' {}
    response {
        status OK()
        body([[
            kitId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        ], [
            kitId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        ]])
    }
},
Contract.make {
    name 'getDataKit'
    'GET ∕dataKits∕'('db1000') {}
    response {
        status OK()
        body(
            kitId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        )
    }
},
Contract.make {
    name 'getDataKitNotFound'
    'GET ∕dataKits∕'('db3000') {}
    response {
        status NOT_FOUND()
        body(
            url    : regex('.*/dataKits/db3000'),
            method : GET,
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

static 'GET ∕dataKits'(@DelegatesTo(Request) Closure $$) {
    $$.owner.delegate.request $$

    $$.delegate.method GET
    $$.delegate.url    "/dataKits"
}
static 'GET ∕dataKits∕'(String id, @DelegatesTo(Request) Closure $$) {
    $$.owner.delegate.request $$

    $$.delegate.method GET
    $$.delegate.url    "/dataKits/$id"
}
