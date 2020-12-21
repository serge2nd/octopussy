import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.http.HttpStatus

import static java.util.Collections.emptyMap as empty
import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
Integer.metaClass.title = { delegate + ' ' + HttpStatus.resolve(delegate).reasonPhrase }

[Contract.make {
    name 'createDataKit'
    'POST ∕dataKits' {
        body(
            kitId: 'db1000',
            properties: [
                url  : 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        )
    }
    response {
        status CREATED()
        body(
            kitId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        )
    }
},
Contract.make {
    name 'createExistingDataKit'
    'POST ∕dataKits' {
        body(
            kitId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        )
    }
    response {
        status BAD_REQUEST()
        body(
            url     : regex(".*/dataKits"),
            method  : POST,
            status  : BAD_REQUEST().title(),
            code    : 'DATA_KIT_EXISTS:db3000',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
},
Contract.make {
    name 'createInvalidDataKit'
    'POST ∕dataKits' {
        body empty()
    }
    response {
        status BAD_REQUEST()
        body(
            url    : regex('.*/dataKits'),
            method : POST,
            status : BAD_REQUEST().title(),
            code   : 'NOT_VALID:dataKitDefinition',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}]

static 'POST ∕dataKits'(@DelegatesTo(Request) Closure $$) {
    $$.owner.delegate.request $$

    $$.delegate.method POST
    $$.delegate.url    '/dataKits'
}