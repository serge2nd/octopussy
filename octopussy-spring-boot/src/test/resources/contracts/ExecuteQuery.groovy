import org.springframework.cloud.contract.spec.Contract

import static java.lang.Integer.MAX_VALUE
import static java.lang.String.format

Contract.make {
    description "must execute query"
    request {
        method POST()
        url "/dataKits/db1000/query"
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
}
