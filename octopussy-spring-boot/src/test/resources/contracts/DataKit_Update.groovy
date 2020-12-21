import org.springframework.cloud.contract.spec.Contract
import org.springframework.cloud.contract.spec.internal.Request
import org.springframework.http.HttpStatus

import static java.util.Collections.emptyMap as empty
import static java.lang.Integer.MAX_VALUE
import static java.lang.String.format
import static org.springframework.cloud.contract.spec.internal.HttpMethods.POST
Integer.metaClass.title = { delegate + ' ' + HttpStatus.resolve(delegate).reasonPhrase }

[Contract.make {
    name 'executeUpdate'
    'POST ∕dataKits∕{ID}∕update'('db1000') {
        body(
            "queries": [[
                "query": "not executed",
                "params": [
                    (''+MAX_VALUE): 0.5
                ]
            ]],
            params: [
                (format("%f", 0.5)): MAX_VALUE
            ]
        )
    }
    response {
        status OK()
        body([5, 7])
    }
},
Contract.make {
    name 'executeUpdateWrong'
    'POST ∕dataKits∕{ID}∕update'('db1000') {
        body empty()
    }
    response {
        status BAD_REQUEST()
        body(
            url    : regex('.*/dataKits/db1000/update'),
            method : POST,
            status : BAD_REQUEST().title(),
            code   : 'NOT_VALID:queries',
            messages: ['err0']
        )
        bodyMatchers {
            jsonPath('$.messages', byCommand($/assertThat((Iterable)$$it).as("$.messages")
                .allElementsMatch(".*\\S+.*")/$))
        }
    }
}]

static 'POST ∕dataKits∕{ID}∕update'(String id, @DelegatesTo(Request) Closure $$) {
    $$.owner.delegate.request $$

    $$.delegate.method POST
    $$.delegate.url    "/dataKits/$id/update"
}