import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must create a data environment'
    request {
        method POST()
        url '/dataEnvironments'
        body(
            envId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        )
    }
    response {
        status CREATED()
        body(
            envId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        )
    }
}
