package pl.ds.websight.rest.framework;

public interface RestAction<T, R> {

    RestActionResult<R> perform(T model);
}
