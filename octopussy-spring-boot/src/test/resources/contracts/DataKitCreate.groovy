import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must create a data kit'
    request {
        method POST()
        url '/dataKits'
        body(
            kitId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
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
}
