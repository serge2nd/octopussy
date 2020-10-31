import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "must get data environment by ID"
    request {
        method GET()
        url '/dataEnvironments/db1000'
    }
    response {
        status OK()
        body(
            envId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        )
    }
}
