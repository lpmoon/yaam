package com.lpmoon.asset.domain.usecase

/**
 * UseCase基类接口
 * @param P 参数类型
 * @param R 返回类型
 */
interface UseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/**
 * 无参数UseCase基类接口
 */
interface UseCaseNoParam<out R> {
    suspend operator fun invoke(): R
}

/**
 * 流式UseCase基类接口
 */
interface FlowUseCase<in P, out R> {
    operator fun invoke(params: P): kotlinx.coroutines.flow.Flow<R>
}

/**
 * 无参数流式UseCase基类接口
 */
interface FlowUseCaseNoParam<out R> {
    operator fun invoke(): kotlinx.coroutines.flow.Flow<R>
}