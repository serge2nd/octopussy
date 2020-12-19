import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "must get data kit by ID"
    request {
        method GET()
        url '/dataKits/db1000'
    }
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
}
