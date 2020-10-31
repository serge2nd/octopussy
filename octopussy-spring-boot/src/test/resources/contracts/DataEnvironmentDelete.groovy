import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must delete data environment'
    request {
        method DELETE()
        url '/dataEnvironments/db1000'
    }
    response {
        status NO_CONTENT()
        body ''
    }
}
