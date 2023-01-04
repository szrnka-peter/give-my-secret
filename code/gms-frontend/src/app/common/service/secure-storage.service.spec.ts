import { TestBed } from "@angular/core/testing";
import { SecureStorageService } from "./secure-storage.service";

describe('SecureStorageService', () => {
    let service: SecureStorageService;

    const configureTestBed = () => {
        TestBed.configureTestingModule({
            providers: [
                SecureStorageService
            ]
        });
        service = TestBed.inject(SecureStorageService);
    };

    it('Should save key', () => {
        // arrange
        configureTestBed();

        // act
        service.setItem('testKey', 'value');

        // assert
        expect(localStorage.getItem('testKey')).toBeDefined();

        localStorage.clear();
    });

    it('Should get empty result', () => {
        // arrange
        configureTestBed();

        // act
        const response : string = service.getItem('testKey');

        // assert
        expect(response).toEqual('');

        localStorage.clear();
    });

    it('Should get key', () => {
        // arrange
        localStorage.setItem('testKey', 'tmTRpmy9eoJIwTsSprlmBw==');
        configureTestBed();

        // act
        const response : string = service.getItem('testKey');

        // assert
        expect(response).toEqual('test');

        localStorage.clear();
    });
});