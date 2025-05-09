#!/usr/bin/env python3

#  -------------------------------------------------------------------------
#  Copyright (C) 2023 Violin Yanev
#  -------------------------------------------------------------------------
#  This Source Code Form is subject to the terms of the Mozilla Public
#  License, v. 2.0. If a copy of the MPL was not distributed with this
#  file, You can obtain one at https://mozilla.org/MPL/2.0/.
#  -------------------------------------------------------------------------


import click

import subprocess
import os
from pathlib import Path
import shutil
import base64
import re


class Common:
    def __init__(self):
        self.script_dir = Path(os.path.realpath(os.path.dirname(__file__)))
        self.root=subprocess.check_output(['git', 'rev-parse', '--show-toplevel'], shell=False, cwd=self.script_dir).decode('utf-8').strip()
        self.image_tag=subprocess.check_output(["git", "describe", "--tags", "--abbrev=0"], shell=False, cwd=self.script_dir).decode('utf-8').strip()
        self.docker_registry = 'ghcr.io'
        self.image_folder = 'image'
        self.container_name = 'violinyanev/my-app-backend'

    def docker_image(self, tag = None):
        if not tag:
            tag = self.image_tag
        return f'{self.docker_registry}/{self.container_name}:{tag}'

    def run_docker(self, args):
        print('Run:', ' '.join(args))
        subprocess.check_call(args, cwd=self.root)



@click.group()
@click.pass_context
def cli(ctx):
    ctx.obj = Common()


def build_impl(conf):
    # make proxy work
    proxy_args = []
    for var in ['HTTP_PROXY', 'HTTPS_PROXY', 'http_proxy', 'https_proxy']:
        if var in os.environ:
            proxy_args += ['--build-arg', f'{var}={os.environ[var]}']
    if proxy_args:
        proxy_args += ['--network', 'host']

    conf.run_docker(['docker', 'build'] +
                    proxy_args +
                     ['-t', conf.docker_image(tag='dev-build'),
                     conf.image_folder])


@cli.command()
@click.pass_obj
def build(conf):
    build_impl(conf)


def start_impl(conf, tag):
    # construct folders
    git_root = (conf.script_dir / '..').resolve()
    base_dir = (git_root / '..').resolve()

    persistent_root = base_dir / f'data-vol'
    print(f'Persistent storage: {persistent_root}')
    os.makedirs(persistent_root, exist_ok=True)
    mounts = ['-v', f'{persistent_root}:/tmp/data']

    # run
    conf.run_docker(['docker', 'run',
                    '-p', '5000:5000',
                     '-i', '-t', '--rm', '--init'] +
                    mounts +
                    [conf.docker_image(tag = tag)])

@cli.command()
@click.pass_obj
@click.option('--tag', help='tag. Default is last tag')
def start(conf, tag):
    start_impl(conf, tag)


@cli.command()
@click.pass_obj
def build_and_run(conf):
    build_impl(conf)
    start_impl(conf)


if __name__ == '__main__':
    try:
        cli()
    except subprocess.CalledProcessError as e:
        print(f'\nCalledProcessError: {e}')
        exit(1)
